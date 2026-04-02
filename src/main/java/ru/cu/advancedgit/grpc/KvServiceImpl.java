package ru.cu.advancedgit.grpc;

import com.google.protobuf.ByteString;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import ru.cu.advancedgit.repository.TarantoolKvRepository;

public class KvServiceImpl extends KvServiceGrpc.KvServiceImplBase {

    private final TarantoolKvRepository repo;

    public KvServiceImpl(TarantoolKvRepository repo) {
        this.repo = repo;
    }

    @Override
    public void get(GetRequest request, StreamObserver<GetResponse> responseObserver) {
        try {
            String key = request.getKey();
            var entryOpt = repo.get(key);

            GetResponse.Builder responseBuilder = GetResponse.newBuilder();

            if (entryOpt.isEmpty()) {
                responseBuilder.setFound(false);
            } else {
                var entry = entryOpt.get();
                responseBuilder
                        .setFound(true)
                        .setKey(entry.getKey());

                if (entry.getValue() != null) {
                    responseBuilder
                            .setHasValue(true)
                            .setValue(ByteString.copyFrom(entry.getValue()));
                } else {
                    responseBuilder.setHasValue(false);
                }
            }

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage())
                    .asException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal server error: " + e.getMessage())
                    .asException());
        }
    }

    @Override
    public void put(PutRequest request, StreamObserver<PutResponse> responseObserver) {
        try {
            String key = request.getKey();
            byte[] value = null;
            if (request.getHasValue()) {
                value = request.getValue().toByteArray();
            }

            repo.put(key, value);

            PutResponse response = PutResponse.newBuilder()
                    .setSuccess(true)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage())
                    .asException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal server error: " + e.getMessage())
                    .asException());
        }
    }

    @Override
    public void delete(DeleteRequest request, StreamObserver<DeleteResponse> responseObserver) {
        try {
            String key = request.getKey();
            boolean deleted = repo.delete(key);

            DeleteResponse response = DeleteResponse.newBuilder()
                    .setDeleted(deleted)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage())
                    .asException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal server error: " + e.getMessage())
                    .asException());
        }
    }

    @Override
    public void count(CountRequest request, StreamObserver<CountResponse> responseObserver) {
        try {
            long count = repo.count();

            CountResponse response = CountResponse.newBuilder()
                    .setCount(count)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal server error: " + e.getMessage())
                    .asException());
        }
    }

    @Override
    public void range(RangeRequest request, StreamObserver<RangeResponse> responseObserver) {
        try {
            String keySince = request.getKeySince();
            String keyTo = request.getKeyTo();
            int limit = request.getLimit();

            var entries = repo.range(keySince, keyTo, limit);

            for (var entry : entries) {
                RangeResponse.Builder responseBuilder = RangeResponse.newBuilder()
                        .setKey(entry.getKey());

                if (entry.getValue() != null) {
                    responseBuilder
                            .setHasValue(true)
                            .setValue(ByteString.copyFrom(entry.getValue()));
                } else {
                    responseBuilder.setHasValue(false);
                }

                responseObserver.onNext(responseBuilder.build());
            }

            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage())
                    .asException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal server error: " + e.getMessage())
                    .asException());
        }
    }
}