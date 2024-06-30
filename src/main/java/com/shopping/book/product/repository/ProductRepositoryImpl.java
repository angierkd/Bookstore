package com.shopping.book.product.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shopping.book.product.dto.ProductListDto;
import com.shopping.book.product.dto.QProductListDto;
import com.shopping.book.product.entity.Product;
import com.shopping.book.product.entity.QProduct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.shopping.book.order.entity.QOrderProduct.orderProduct;
import static com.shopping.book.order.entity.QOrders.orders;
import static com.shopping.book.product.entity.QCategory.category;
import static com.shopping.book.product.entity.QProduct.product;



@Slf4j
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;

    public ProductRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
        this.entityManager = entityManager;
    }

    public LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
    public List<ProductListDto> productIds = null;

    public NumberExpression<Integer> totalQuantity = new CaseBuilder()
            .when(orders.date.goe(oneMonthAgo)).then(orderProduct.quantity)
            .otherwise(0).sum();

    QProductListDto qProductListDto = new QProductListDto(
            product.id,
            product.image,
            product.name,
            product.price,
            product.publisher,
            product.writer,
            totalQuantity
    );

    QProductListDto qProductListDto2 = new QProductListDto(
            product.id,
            product.image,
            product.name,
            product.price,
            product.publisher,
            product.writer
    );

    @Override
    public Page<ProductListDto> searchProductsInCategory(Pageable pageable, Long categoryId, String sort) {

        BooleanExpression categoryEq = product.category.id.eq(categoryId).or(category.parentCategory.id.eq(categoryId));

        if ("popularity".equalsIgnoreCase(sort)) {
            //인기순
            productIds = queryFactory
                    .select(qProductListDto)
                    .from(product)
                    .leftJoin(orderProduct).on(orderProduct.product.id.eq(product.id))
                    .leftJoin(orders).on(orders.id.eq(orderProduct.orders.id))
                    .leftJoin(category).on(category.id.eq(product.category.id))
                    .where(categoryEq)
                    .groupBy(product.id)
                    .orderBy(totalQuantity.desc()) // 주문 수량의 합에 대해 내림차순 정렬
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .fetch();
        } else if ("price".equalsIgnoreCase(sort)) {
            //저렴한
            productIds = queryFactory.select(qProductListDto2)
                    .from(product)
                    .leftJoin(category).on(category.id.eq(product.category.id))
                    .where(categoryEq)
                    .orderBy(product.price.asc())
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .fetch();
        }

        // 총 개수 쿼리
        Long total = queryFactory
                .select(product.count())
                .from(product)
                .leftJoin(category).on(category.id.eq(product.category.id))
                .where(categoryEq)
                .fetchOne();

        // null 체크 및 기본값 처리
        long totalElements = (total != null) ? total : 0L;
        return new PageImpl<>(productIds, pageable, totalElements);
    }

    @Override
    public Page<ProductListDto> searchProductsInQuery(String searchQuery, String sort, Pageable pageable) {

        BooleanExpression search = product.name.like("%" + searchQuery + "%");

        if ("popularity".equalsIgnoreCase(sort)) {

            //인기순
            productIds = queryFactory
                    .select(qProductListDto)
                    .from(product)
                    .leftJoin(orderProduct).on(product.id.eq(orderProduct.product.id))
                    .leftJoin(orders).on(orders.id.eq(orderProduct.orders.id))
                    .where(search)
                    .groupBy(product.id)
                    .orderBy(totalQuantity.desc()) // 주문 수량의 합에 대해 내림차순 정렬
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .fetch();
        } else if ("price".equalsIgnoreCase(sort)) {
            //저렴한
            productIds = queryFactory.select(qProductListDto2)
                    .from(product)
                    .where(search)
                    .orderBy(product.price.asc())
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .fetch();
        }

        Long total = queryFactory
                .select(product.count())
                .from(product)
                .where(search)
                .fetchOne();

        // null 체크 및 기본값 처리
        long totalElements = (total != null) ? total : 0L;
        return new PageImpl<>(productIds, pageable, totalElements);
}


    @Override
    public Optional<Product> findByIdWithPessimisticLock(Long id) {
        QProduct product = QProduct.product;

        Product result = queryFactory.selectFrom(product)
                .where(product.id.eq(id))
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetchOne();

        return Optional.ofNullable(result);
    }
}
