package music.mapper;

import music.model.Device;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DeviceMapper {

    void insert(@Param("name") String name);

    Device getDeviceByName(@Param("name") String name);
}
