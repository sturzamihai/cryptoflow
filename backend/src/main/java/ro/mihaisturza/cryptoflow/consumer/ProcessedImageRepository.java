package ro.mihaisturza.cryptoflow.consumer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedImageRepository extends JpaRepository<ProcessedImage, String> {    
}
