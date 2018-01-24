package com.asholokh.twofa.googleauth.sample.service;

import com.asholokh.twofa.googleauth.sample.totp.TOTP;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class TotpService {
    public boolean verifyCode(String totpCode, String secret) {
        String totpCodeBySecret = TOTP.generateTOTP(Hex.encodeHexString(secret.getBytes()), String.valueOf(System.currentTimeMillis() / 1000L), "6");
        return totpCodeBySecret.equals(totpCode);
    }
}
