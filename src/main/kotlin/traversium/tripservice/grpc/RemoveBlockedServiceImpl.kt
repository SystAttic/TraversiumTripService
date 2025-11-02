package traversium.tripservice.grpc

import io.grpc.stub.StreamObserver
import org.springframework.grpc.server.service.GrpcService
import traversium.tripservice.removeblocked.RemoveBlockedServiceGrpc
import traversium.tripservice.removeblocked.RemoveRequest
import traversium.tripservice.removeblocked.RemoveResponse
import traversium.tripservice.service.TripService


@GrpcService
class TripCleanupGrpcService(
    private val tripService: TripService
) : RemoveBlockedServiceGrpc.RemoveBlockedServiceImplBase() {

    override fun removeBlockedUserRelations(
        request: RemoveRequest, responseObserver: StreamObserver<RemoveResponse?>?
    ) {
        try {
            val message = tripService.removeBlockedUserRelations(
                blockerId = request.blockerId,
                blockedId = request.blockedId
            )

            val response = RemoveResponse.newBuilder()
                .setMessage(message)
                .build()

            responseObserver?.onNext(response)
            responseObserver?.onCompleted()
        } catch (e: Exception) {
            responseObserver?.onError(e)
        }
    }

}
