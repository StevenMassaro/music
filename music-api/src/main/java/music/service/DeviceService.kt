package music.service

import music.mapper.DeviceMapper
import music.model.Device
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class DeviceService @Autowired
constructor(private val deviceMapper: DeviceMapper) {

    /**
     * If a device with the specified name already exists, return it, otherwise, insert
     * it and then return the newly inserted object.
     */
    fun getOrInsert(name: String): Device {
        val device = getDeviceByName(name)
		return if (device == null) {
			deviceMapper.insert(name)
			getDeviceByName(name)!!
		} else {
			device
		}
    }

	fun getDeviceByName(name: String) : Device? = deviceMapper.getDeviceByName(name)
}
