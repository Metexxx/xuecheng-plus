package com.xuecheng;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class BCryptTest {
    public static void main(String[] args) {
        String password = "111111";
        PasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        for (int i = 0; i < 10; i++) {
            // 编码10次 每次计算出来的Hash值都不一样
            String encode = bCryptPasswordEncoder.encode(password);
            System.out.println(encode);
            // 虽然每次计算的Hash值都不一样,但是每次计算出来的Hash值都是同一个密码的Hash值
            // 所以每次计算出来的Hash值都可以用来验证密码是否正确
            boolean matches = bCryptPasswordEncoder.matches(password, encode);
            System.out.println(matches);
        }
    }
}
