package org.almart.microservices.core.recommendation.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import org.almart.api.core.recommendation.Recommendation;
import org.almart.api.core.recommendation.RecommendationService;
import org.almart.microservices.core.recommendation.persistence.RecommendationEntity;
import org.almart.microservices.core.recommendation.persistence.RecommendationRepository;
import org.almart.util.exceptions.InvalidInputException;
import org.almart.util.http.ServiceUtil;
import reactor.core.publisher.Flux;




@RestController
public class RecommendationServiceImpl implements RecommendationService {

    private static final Logger LOG = LoggerFactory.getLogger(RecommendationServiceImpl.class);

    private final RecommendationRepository repository;

    private final RecommendationMapper mapper;

    private final ServiceUtil serviceUtil;

    @Autowired
    public RecommendationServiceImpl(RecommendationRepository repository, RecommendationMapper mapper, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Recommendation createRecommendation(Recommendation body) {

        if (body.getProductId() < 1) throw new InvalidInputException("Invalid productId: " + body.getProductId());
        RecommendationEntity entity = mapper.apiToEntity(body);
        return repository.save(entity)
                .log()
                .onErrorMap(
                        DuplicateKeyException.class,
                        dy -> new DuplicateKeyException("Dublicate Key: " + entity.getRecommendationId())
                        )
                .map(mapper::entityToApi)
                .block();
    }

    @Override
    public Flux<Recommendation> getRecommendations(int productId) {

        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        return repository.findByProductId(productId)
                .log()
                .map(mapper::entityToApi)
                .map(e -> {e.setServiceAddress(serviceUtil.getServiceAddress()); return e;});
    }

    @Override
    public void deleteRecommendations(int productId) {
        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        LOG.debug("deleteRecommendations: tries to delete recommendations for the product with productId: {}", productId);
        repository.deleteAll(repository.findByProductId(productId)).block();
    }
}
