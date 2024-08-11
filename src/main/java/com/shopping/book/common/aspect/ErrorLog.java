package com.shopping.book.common.aspect;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Document(indexName = "error-logs")
public class ErrorLog {

    @Id
    private String id;
    private String exceptionMessage;  // 예외 메시지
    private String stackTrace;        // 스택 트레이스
    private String serviceName;       // 서비스 이름
    private String methodName;        // 메서드 이름
    private String arguments;         // 전달된 인자들
    private Date timestamp;           // 타임스탬프

    // 기본 생성자
    public ErrorLog() {
        this.timestamp = new Date();  // 생성 시점의 타임스탬프 설정
    }

    // 모든 필드를 포함한 생성자
    public ErrorLog(String exceptionMessage, String stackTrace, String serviceName, String methodName, String arguments) {
        this.exceptionMessage = exceptionMessage;
        this.stackTrace = stackTrace;
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.arguments = arguments;
        this.timestamp = new Date();  // 생성 시점의 타임스탬프 설정
    }
}