package edu.nd.pmcburne.hello

class CampusRepo(
    private val locationDao: LocationDataAccessObject
) {

    suspend fun refreshLocations(): List<LocationEntity> {
        return try {
            val placemarks = CampusApi.retrofitService.getPlacemarks()

            val entities = placemarks.map { placemark ->
                LocationEntity(
                    id = placemark.id,
                    name = placemark.name,
                    description = placemark.description,
                    latitude = placemark.visualCenter.latitude,
                    longitude = placemark.visualCenter.longitude,
                    tags = placemark.tagList.joinToString(",")
                )
            }

            locationDao.upsertAll(entities)
            locationDao.getAllLocations()
        } catch (e: Exception) {
            locationDao.getAllLocations()
        }
    }

    suspend fun getStoredLocations(): List<LocationEntity> {
        return locationDao.getAllLocations()
    }
}