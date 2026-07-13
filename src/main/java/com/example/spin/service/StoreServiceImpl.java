package com.example.spin.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.spin.domain.Store;
import com.example.spin.exception.NotFoundException;
import com.example.spin.repository.StoreRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private static final int QR_SIZE = 300;

    private final StoreRepository storeRepository;

    // 앱에 등록해둔 커스텀 URL 스킴. QR을 찍으면 이 스킴으로 앱이 바로 열린다.
    @Value("${app.deep-link-scheme:spin}")
    private String deepLinkScheme;

    @Override
    public byte[] generateQrCodeImage(int storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new NotFoundException("가게를 찾을 수 없습니다: " + storeId));

        String deepLink = deepLinkScheme + "://checkin?code=" + store.getQrCode();

        try {
            BitMatrix matrix = new QRCodeWriter()
                    .encode(deepLink, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return out.toByteArray();
        } catch (WriterException | IOException e) {
            throw new IllegalStateException("QR 코드 생성에 실패했습니다: storeId=" + storeId, e);
        }
    }
}
