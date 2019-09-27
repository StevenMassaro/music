package music.service;

import music.mapper.DeviceMapper;
import music.model.Device;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeviceService {

    private final DeviceMapper deviceMapper;

    @Autowired
    public DeviceService(DeviceMapper deviceMapper) {
        this.deviceMapper = deviceMapper;
    }

    /**
     * If a device with the specified name already exists, return it, otherwise, insert
     * it and then return the newly inserted object.
     */
    public Device getOrInsert(String name) {
        Device device = deviceMapper.getDeviceByName(name);
        if (device == null) {
            deviceMapper.insert(name);
            return deviceMapper.getDeviceByName(name);
        } else {
            return device;
        }
    }
}
