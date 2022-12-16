package co.project.api.recap.service;

import co.project.api.recap.model.User;
import co.project.api.recap.repository.RecapRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RecapService {

    @Autowired
    RecapRepository recapRepository;

    public void saveUser(User user) {
        recapRepository.save(user);
    }
}
