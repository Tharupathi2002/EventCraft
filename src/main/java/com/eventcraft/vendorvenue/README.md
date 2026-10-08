# Module: Vendor & Venue Management
Owner: Alvitigala H.M. (IT25102929)
Proposal section: 6.3

Suggested layout (same pattern as Sujeewan's `eventplanning` package):
- entity/      -> JPA entities (e.g. Vendor.java, VenueBooking.java)
- dto/         -> request/response DTOs
- repository/  -> Spring Data JPA repositories
- service/     -> interface + Impl business logic
- controller/  -> @RestController REST endpoints (suggested base path: /api/vendors)
- exception/   -> custom exceptions + @RestControllerAdvice

Delete this README once you add your own files.
