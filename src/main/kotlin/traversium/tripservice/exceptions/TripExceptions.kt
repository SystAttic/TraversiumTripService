package traversium.tripservice.exceptions

class TripNotFoundException(id: Long) : RuntimeException("Trip with ID $id not found")
class TripAlreadyExistsException(id: Long) : RuntimeException("Trip with ID '$id' already exists")
class TripWithoutAlbumsException(id: Long) : RuntimeException("Trip with ID '$id' has no albums")