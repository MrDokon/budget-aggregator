package com.project.dokon.budgetaggregator.importing.repository;

import com.project.dokon.budgetaggregator.importing.model.ImportJob;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportJobRepository extends MongoRepository<ImportJob, String> {

}
