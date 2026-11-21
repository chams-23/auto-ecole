package com.autoecole.app.repositories;

import com.autoecole.app.entites.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByDestinataireId(Long destinataireId);

    List<Notification> findAllByTenantIdAndDestinataireId(Long tenantId, Long destinataireId);

    Optional<Notification> findByIdAndDestinataireId(Long id, Long destinataireId);

    Optional<Notification> findByTenantIdAndIdAndDestinataireId(Long tenantId, Long id, Long destinataireId);

    List<Notification> findAllByDestinataireIdAndStatutLecture(Long destinataireId, Boolean statutLecture);

    List<Notification> findAllByTenantIdAndDestinataireIdAndStatutLecture(Long tenantId, Long destinataireId, Boolean statutLecture);

    List<Notification> findAllByDestinataireIdAndTypeEvenement(Long destinataireId, com.autoecole.app.enums.TypeEvenementNotification typeEvenement);
}
