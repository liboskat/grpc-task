package ru.kpfu.grpctask.client;

import ru.kpfu.grpctask.proto.math.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MathClient {
    String ipAddress = "localhost";
    int port = 5052;
    Scanner sc;

    public static void main(String[] args) {
        System.out.println("gRPC Client is started");
        MathClient main = new MathClient();
        main.run();
    }

    private void run() {
        sc = new Scanner(System.in);
        System.out.println("Числа вводятся с разделителем - запятой или без разделителя " +
                "(при подсчете множителей только без разделителя)");
        //gRPC provides a channel construct which abstracts out the underlying details like connection, connection pooling, load balancing, etc.
        ManagedChannel channel = ManagedChannelBuilder.forAddress(ipAddress, port)
                .usePlaintext()
                .build();

        //unary implementation
        doGetSqrtCall(channel);

        //client streaming implementation
        doGetSTDCall(channel);

        //server streaming implementation
        doGetFactorsCall(channel);

        //bi directional streaming implementation
        doGetMaxCall(channel);

        channel.shutdown();
    }


    private void doGetSqrtCall(ManagedChannel channel) {
        System.out.println("*** Unary implementation ***");

        MathServiceGrpc.MathServiceBlockingStub client = MathServiceGrpc.newBlockingStub(channel);

        System.out.println("Введите число");
        double number = sc.nextDouble();

        SqrtRequest request = SqrtRequest.newBuilder()
                .setNumber(number)
                .build();
        System.out.println("Отправлено число: " + number);

        SqrtResponse response = client.getSqrt(request);

        System.out.println("Корень числа: " + response.getResult());
    }

    private void doGetSTDCall(ManagedChannel channel) {
        System.out.println("*** Client streaming implementation ***");

        MathServiceGrpc.MathServiceStub client = MathServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<STDRequest> requestObserver = client.getSTD(new StreamObserver<STDResponse>() {
            @Override
            public void onNext(STDResponse stdResponse) {
                System.out.println("Стандартное отклонение: " + stdResponse.getResult());
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        System.out.println("Введите 5 чисел");
        for (int i = 0; i < 5; i++) {
            double number = sc.nextDouble();
            requestObserver.onNext(STDRequest.newBuilder()
                    .setNumber(number)
                    .build());
            System.out.println("Отправлено число " + number);
        }

        requestObserver.onCompleted();

        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doGetFactorsCall(ManagedChannel channel) {
        System.out.println("*** Server streaming implementation ***");

        MathServiceGrpc.MathServiceBlockingStub client = MathServiceGrpc.newBlockingStub(channel);

        System.out.println("Введите число");
        int number = sc.nextInt();

        FactorsRequest factorsRequest = FactorsRequest.newBuilder()
                .setNumber(number)
                .build();
        System.out.println("Отправлено число " + number);
        client.getFactors(factorsRequest)
                .forEachRemaining(greetManyTimesResponse ->
                        System.out.println("Множитель числа: " + greetManyTimesResponse.getResult()));
    }

    private void doGetMaxCall(ManagedChannel channel) {
        System.out.println("*** Bi directional streaming implementation ***");

        MathServiceGrpc.MathServiceStub client = MathServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<MaxRequest> requestObserver = client.getMax(new StreamObserver<MaxResponse>() {
            @Override
            public void onNext(MaxResponse maxResponse) {
                System.out.println("Максимум ряда чисел: " + maxResponse.getResult());
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        System.out.println("Введите 5 чисел");
        for (int i = 0; i < 5; i++) {
            double number = sc.nextDouble();
            requestObserver.onNext(MaxRequest.newBuilder()
                    .setNumber(number)
                    .build());
            System.out.println("Отправлено число " + number);

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        requestObserver.onCompleted();

        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
