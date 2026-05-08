package com.eattea.mapper;

import com.eattea.entity.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MemberMapper {

    List<Member> selectAll();

    Member selectById(@Param("id") Long id);

    int insert(Member member);

    int update(Member member);

    int deleteById(@Param("id") Long id);
}
