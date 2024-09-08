package cn.net.bhe.mongodbdemo.dao;

import cn.net.bhe.mongodbdemo.dao.po.UserPO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<UserPO, String> {

}
