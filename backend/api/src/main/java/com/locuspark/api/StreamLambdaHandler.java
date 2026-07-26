package com.locuspark.api;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.HttpApiV2ProxyRequest; // ✅ Alterado para V2
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.serverless.proxy.internal.LambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamLambdaHandler implements RequestStreamHandler {
    // ✅ Atualizado o tipo genérico para HttpApiV2ProxyRequest
    private static final SpringBootLambdaContainerHandler<HttpApiV2ProxyRequest, AwsProxyResponse> handler;

    static {
        LambdaContainerHandler.getContainerConfig().addBinaryContentTypes(
                "application/pdf",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "text/csv");
        try {
            // ✅ Alterado de getAwsProxyHandler para getHttpApiV2ProxyHandler
            handler = SpringBootLambdaContainerHandler.getHttpApiV2ProxyHandler(ApiApplication.class);
        } catch (ContainerInitializationException e) {
            throw new RuntimeException("Não foi possível inicializar o container do Spring Boot", e);
        }
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        handler.proxyStream(inputStream, outputStream, context);
    }
}