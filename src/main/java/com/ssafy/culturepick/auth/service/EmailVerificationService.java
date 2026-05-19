package com.ssafy.culturepick.auth.service;

import com.ssafy.culturepick.auth.repository.EmailVerificationRepository;
import com.ssafy.culturepick.global.exception.code.AuthErrorCode;
import com.ssafy.culturepick.global.exception.type.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final JavaMailSender javaMailSender;
    private final EmailVerificationRepository emailVerificationRepository;

    public void sendCode(String email) {
        emailVerificationRepository.deleteVerified(email);

        String code = generateCode();
        emailVerificationRepository.saveCode(email, code);
        sendEmail(email, code);
    }

    public void verifyCode(String email, String code) {
        String savedCode = emailVerificationRepository.getCode(email);

        if (savedCode == null) {
            throw new BusinessException(AuthErrorCode.EXPIRED_EMAIL_CODE);
        }

        if (!savedCode.equals(code)) {
            throw new BusinessException(AuthErrorCode.MISMATCHED_EMAIL_CODE);
        }

        emailVerificationRepository.deleteCode(email);
        emailVerificationRepository.saveVerified(email);
    }

    public void deleteVerified(String email) {
        emailVerificationRepository.deleteVerified(email);
    }

    private String generateCode() {
        return String.format("%06d", new Random().nextInt(1_000_000));
    }

    private void sendEmail(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[CulturePick] 이메일 인증 코드");
        message.setText("인증 코드: " + code + "\n5분 내에 입력해주세요.");
        javaMailSender.send(message);
    }
}
