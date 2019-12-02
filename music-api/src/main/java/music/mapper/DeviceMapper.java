package music.mapper;

import music.model.Device;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface DeviceMapper {

    void insert(@Param("name") String name);

    Device getDeviceByName(@Param("name") String name);
}
