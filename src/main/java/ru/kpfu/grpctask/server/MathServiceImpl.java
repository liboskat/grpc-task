package ru.kpfu.grpctask.server;

import ru.kpfu.grpctask.proto.math.*;
import io.grpc.stub.StreamObserver;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

public class MathServiceImpl extends MathServiceGrpc.MathServiceImplBase {
    @Override
    public void getSqrt(SqrtRequest request, StreamObserver<SqrtResponse> responseObserver) {
        System.out.println("*** Unary implementation on server side ***");

        SqrtResponse response = SqrtResponse.newBuilder()
                .setResult(Math.sqrt(request.getNumber()))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<STDRequest> getSTD(StreamObserver<STDResponse> responseObserver) {
        System.out.println("*** Client streaming implementation on server side ***");

        return new StreamObserver<STDRequest>() {
            List<Double> numbers = new ArrayList<>();

            @Override
            public void onNext(STDRequest stdRequest) {
                numbers.add(stdRequest.getNumber());
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onCompleted() {
                double average = numbers.stream().mapToDouble(Double::doubleValue).average().orElse(0);
                double dispersion = numbers.stream().mapToDouble(Double::doubleValue)
                        .map(number -> number - average)
                        .map(deviation -> deviation * deviation)
                        .average()
                        .orElse(0);
                responseObserver.onNext(STDResponse.newBuilder()
                        .setResult(Math.sqrt(dispersion))
                        .build());
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void getFactors(FactorsRequest request, StreamObserver<FactorsResponse> responseObserver) {
        System.out.println("*** Server streaming implementation on server side ***");

        int number = request.getNumber();
        try {
            for (int probe = 2; probe < Math.sqrt(number); probe++) {
                if (number % probe == 0) {
                    responseObserver.onNext(FactorsResponse.newBuilder()
                            .setResult(probe)
                            .build());
                    Thread.sleep(1000L);

                    responseObserver.onNext(FactorsResponse.newBuilder()
                            .setResult(number / probe)
                            .build());
                    Thread.sleep(1000L);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public StreamObserver<MaxRequest> getMax(StreamObserver<MaxResponse> responseObserver) {
        System.out.println("*** Bi directional streaming implementation on server side ***");

        return new StreamObserver<MaxRequest>() {
            double max = 0;

            @Override
            public void onNext(MaxRequest value) {
                double number = value.getNumber();
                if (number > max) {
                    max = number;
                }
                responseObserver.onNext(MaxResponse.newBuilder()
                        .setResult(max)
                        .build());
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }
}
