package gdou.laiminghai.jwtclient.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
public class AuthController {

    @Value("${jwt.secret.key}")
    private String secretKey;

    @Value("${token.expire.time}")
    private long tokenExpireTime;

    @Value("${refresh.token.expire.time}")
    private long refreshTokenExpireTime;

    @Value("${jwt.refresh.token.key.format}")
    private String jwtRefreshTokenKeyFormat;

    @Value("${jwt.blacklist.key.format}")
    private String jwtBlacklistKeyFormat;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 登录授权，生成JWT
     * @param userName
     * @param password
     * @return
     */
    @PostMapping("/auth")
    public Map<String,Object> login(@RequestParam String userName,
                                    @RequestParam String password){
        Map<String,Object> resultMap = new HashMap<>();
        //账号密码校验
        if(equals(userName, "admin")&&
                equals(password, "admin")){

            //生成JWT
            String token = buildJWT(userName);
            //生成refreshToken
            String refreshToken = UUID.randomUUID().toString().replaceAll("-","");
            //保存refreshToken至redis，使用hash结构保存使用中的token以及用户标识
            String refreshTokenKey = String.format(jwtRefreshTokenKeyFormat, refreshToken);
            stringRedisTemplate.opsForHash().put(refreshTokenKey,
                    "token", token);
            stringRedisTemplate.opsForHash().put(refreshTokenKey,
                    "userName", userName);
            //refreshToken设置过期时间
            stringRedisTemplate.expire(refreshTokenKey,
                    refreshTokenExpireTime, TimeUnit.MILLISECONDS);
            //返回结果
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("token", token);
            dataMap.put("refreshToken", refreshToken);
            resultMap.put("code", "10000");
            resultMap.put("data", dataMap);
            return resultMap;
        }
        resultMap.put("isSuccess", false);
        return resultMap;
    }

    /**
     * 刷新JWT
     * @param refreshToken
     * @return
     */
    @GetMapping("/token/refresh")
    public Map<String,Object> refreshToken(@RequestParam String refreshToken){
        Map<String,Object> resultMap = new HashMap<>();
        String refreshTokenKey = String.format(jwtRefreshTokenKeyFormat, refreshToken);
        String userName = (String)stringRedisTemplate.opsForHash().get(refreshTokenKey,
                "userName");
        if(isBlank(userName)){
            resultMap.put("code", "10001");
            resultMap.put("msg", "refreshToken过期");
            return resultMap;
        }
        String newToken = buildJWT(userName);
        //替换当前token，并将旧token添加到黑名单
        String oldToken = (String)stringRedisTemplate.opsForHash().get(refreshTokenKey,
                "token");
        stringRedisTemplate.opsForHash().put(refreshTokenKey, "token", newToken);
        stringRedisTemplate.opsForValue().set(String.format(jwtBlacklistKeyFormat, oldToken), "",
                tokenExpireTime, TimeUnit.MILLISECONDS);
        resultMap.put("code", "10000");
        resultMap.put("data", newToken);
        return resultMap;
    }

    private String buildJWT(String userName){
        //生成jwt
        Date now = new Date();
        Algorithm algo = Algorithm.HMAC256(secretKey);
        String token = JWT.create()
                .withIssuer("MING")
                .withIssuedAt(now)
                .withExpiresAt(new Date(now.getTime() + tokenExpireTime))
                .withClaim("userName", userName)//保存身份标识
                .sign(algo);
        return token;
    }

    public static boolean isBlank(CharSequence cs)
    {
        int strLen;

        if ((cs == null) || ((strLen = cs.length()) == 0))
            return true;
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    public boolean equals(String str1, String str2) {
        return str1 == null ? str2 == null : str1.equals(str2);
    }
}