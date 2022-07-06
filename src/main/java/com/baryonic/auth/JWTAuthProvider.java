package com.baryonic.auth;

import io.helidon.security.*;
import io.helidon.security.spi.AuthenticationProvider;
import io.helidon.security.spi.AuthorizationProvider;
import io.helidon.security.spi.SynchronousProvider;
import io.helidon.security.util.TokenHandler;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.EcKeyUtil;
import redis.clients.jedis.JedisPool;

public class JWTAuthProvider extends SynchronousProvider implements AuthenticationProvider, AuthorizationProvider
{
    private static final String JWT_TOKEN_HEADER = "Authorization";
    private static final String JWT_TOKEN_PREFIX = "Bearer ";

    private final JedisPool pool;
    private final TokenHandler tokenHandler;

    public JWTAuthProvider(JedisPool jedisPool)
    {
        this.pool = jedisPool;
        this.tokenHandler = TokenHandler.builder()
                                        .tokenHeader(JWT_TOKEN_HEADER)
                                        .tokenPrefix(JWT_TOKEN_PREFIX)
                                        .build();
    }

    @Override
    protected AuthenticationResponse syncAuthenticate(ProviderRequest providerRequest)
    {
        var env = providerRequest.env();
        var headers = env.headers();

        try
        {
            return this.tokenHandler.extractToken(headers)
                                    .map(this::authenticateToken)
                                    .orElseGet(() -> AuthenticationResponse.failed("Invalid or missing JWT token!"));
        }
        catch (Exception e)
        {
            return AuthenticationResponse.failed("JWT header not available or in a wrong format.");
        }
    }

    private AuthenticationResponse authenticateToken(String token)
    {
        try
        {
            var jwtConsumer = new JwtConsumerBuilder()
                .setRequireExpirationTime()
                .setAllowedClockSkewInSeconds(60)
                .setRequireSubject()
                .setExpectedIssuer("baryonic.auth")
                .setExpectedAudience("baryonic.auth")
                .setVerificationKeyResolver((jws, nestingContext) -> {
                    var keyID = jws.getKeyIdHeaderValue();
                    try (var jedis = this.pool.getResource())
                    {
                        var key = "baryonic:public_keys:" + keyID;
                        return new EcKeyUtil().fromPemEncoded(jedis.get(key));
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException(e);
                    }
                })
                .setJwsAlgorithmConstraints(AlgorithmConstraints.ConstraintType.PERMIT, AlgorithmIdentifiers.ECDSA_USING_P384_CURVE_AND_SHA384)
                .build();

            var claims = jwtConsumer.processToClaims(token);

            var keyRefID = claims.getSubject();
            long userID;

            try (var jedis = this.pool.getResource())
            {
                var key = "baryonic:jwt:" + keyRefID;
                userID = Long.parseUnsignedLong(jedis.get(key));
            }
            catch (Exception e)
            {
                return fail("Invalid or revoked JWT subject mapping!");
            }

            var principal = Principal.builder()
                .id(Long.toUnsignedString(userID))
                .build();

            var userGrant = Role.builder()
                                 .name("user")
                                 .build();

            var subject = Subject.builder()
                                 .principal(principal)
                                 .addGrant(userGrant)
                                 .addAttribute("token", token)
                                 .addAttribute("key_ref_id", keyRefID)
                                 .build();

            return AuthenticationResponse.success(subject);
        }
        catch (InvalidJwtException | MalformedClaimException e)
        {
            return fail("Invalid or broken JWT token!");
        }
    }

    @Override
    protected AuthorizationResponse syncAuthorize(ProviderRequest providerRequest)
    {
        return AuthorizationResponse.permit();
    }

    private static AuthenticationResponse fail(String reason)
    {
        return AuthenticationResponse.builder()
                                     .status(SecurityResponse.SecurityStatus.FAILURE)
                                     .description(reason)
                                     .build();
    }
}
