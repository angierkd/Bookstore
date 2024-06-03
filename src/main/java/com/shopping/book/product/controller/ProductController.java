package com.shopping.book.product.controller;

import com.shopping.book.product.dto.ProductListDto;
import com.shopping.book.product.entity.Product;
import com.shopping.book.product.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@AllArgsConstructor
@Controller
@RequestMapping("/product")
public class ProductController {

    private ProductService productservice;

    //메인페이지
    @GetMapping("/homepage")
    public String homepage(@PageableDefault(size = 6) Pageable pageable, Model model){
        Page<ProductListDto> list = productservice.getProductsBySearch("", "popularity", pageable);
        model.addAttribute("products", list);
        return "home";
    }

    //카테고리 + 인기순/낮은 가격순으로 상품 검색
    @GetMapping("/search/category")
    public String getProductsByCategory (@RequestParam(name="category") Long  categoryId,
                           @RequestParam(name="sort", defaultValue = "popularity") String sort,
                           @PageableDefault(size = 3) Pageable pageable,
                           Model model, HttpServletRequest request){

        log.info("categoryId: {}  sort  {}", categoryId, sort);

        Page<ProductListDto> list = productservice.getProductsByCategory(pageable, categoryId, sort);

        model.addAttribute("products", list);
        model.addAttribute("category", categoryId);
        model.addAttribute("sort", sort);
        model.addAttribute("currentPath", request.getRequestURI());
        return "product-list";
    }

    //검색어 + 인기순/낮은 가격순으로 상품 검색
    @GetMapping("/search/query")
    public String getProductsBySearch(@RequestParam(name="query") String query,
                           @RequestParam(name="sort", defaultValue = "popularity") String sort,
                           @PageableDefault(size = 3) Pageable pageable,
                           Model model,HttpServletRequest request){

        log.info("query: {}  sort  {}", query, sort);

        Page<ProductListDto> list = productservice.getProductsBySearch(query, sort, pageable);

        model.addAttribute("products", list);
        model.addAttribute("query", query);
        model.addAttribute("sort", sort);
        model.addAttribute("currentPath", request.getRequestURI());
        return "product-list";
    }

    //상품 상세 페이지
    @GetMapping("/{id}")
    public String getProductDetails(@PathVariable("id") Long id, Model model){
        Product product = productservice.getProductDetails(id);
        model.addAttribute("product", product);
        return "product";
    }

}
