package com.example.spin.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.spin.service.StoreService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stores")
@Tag(name = "가게", description = "가맹점 QR 코드 (로그인 불필요, 인쇄용)")
public class StoreController {

    private final StoreService storeService;

    @Operation(summary = "가게 QR 코드 이미지 조회", description = "해당 가게의 qr_code 값을 인코딩한 PNG 이미지를 반환한다.")
    @GetMapping(value = "/{storeId}/qrcode", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] getQrCode(@PathVariable int storeId) {
        return storeService.generateQrCodeImage(storeId);
    }
}
