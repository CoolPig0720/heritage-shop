package com.heritage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heritage.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select({
            "<script>",
            "SELECT id, name",
            "FROM `user`",
            "WHERE id IN",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    List<User> selectBatchIdsFromUserTable(@Param("ids") Collection<Long> ids);
}
