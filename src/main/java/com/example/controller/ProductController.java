package com.example.controller;

import com.example.model.Product;
import com.example.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Controller
public class ProductController {

    private static final int[] PAGE_SIZES = {2, 5, 7, 10};

    @Autowired
    private ProductRepository repo;

    @RequestMapping("/")
    public String viewHomePage(Model model) {
        // String keyword = "reebok";
        String keyword = null;

        /*
		 * if (keyword != null) { return listByPage(model, 1, "name", "asc", keyword); }
         */
        return listByPage(model, 1, 2, "name", "asc", keyword);

    }

    @GetMapping("/page/{pageNumber}")
    public String listByPage(Model model, @PathVariable("pageNumber") int currentPage, @RequestParam("pageSize") int pageSize,
            @RequestParam("sortField") String sortField, @RequestParam("sortDir") String sortDir, @RequestParam(value = "keyword", required = false) String keyword) {

        Sort sort = Sort.by(sortField);
        sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();

        Pageable pageable = PageRequest.of(currentPage - 1, pageSize, sort); // 7 rows per page
        Page<Product> page = null;
        if (keyword != null) {
            page = repo.findAllByNameContainingOrBrandContainingOrMadeinContaining(keyword, keyword, keyword, pageable);
        } else {
            page = repo.findAll(pageable);
        }

        long totalItems = page.getTotalElements();
        int totalPages = page.getTotalPages();
        int numberOfElements = page.getNumberOfElements();
        // int currentPage = page.previousPageable().getPageNumber();

        List<Product> listProducts = page.getContent();

        model.addAttribute("totalItems", totalItems);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("pageSizes", PAGE_SIZES);
        model.addAttribute("numberOfElements", numberOfElements);
        model.addAttribute("listProducts", listProducts); // next bc of thymeleaf we make the index.html

        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("keyword", keyword);

        String reverseSortDir = sortDir.equals("asc") ? "desc" : "asc";
        model.addAttribute("reverseSortDir", reverseSortDir);

        return "index";
    }

    @RequestMapping("/new")
    public String showNewProductForm(Model model) {
        Product product = new Product();
        model.addAttribute("product", product);

        return "new_product";
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String saveProduct(@ModelAttribute("product") Product product) {
        repo.save(product);

        return "redirect:/";
    }

    @RequestMapping("/edit/{id}")
    public ModelAndView showEditProductForm(@PathVariable(name = "id") Long id) {
        ModelAndView modelAndView = new ModelAndView("edit_product");
        Product product = repo.findById(id).get();
        modelAndView.addObject("product", product);

        return modelAndView;
    }

    @RequestMapping("/delete/{id}")
    public String deleteProduct(@PathVariable(name = "id") Long id) {
        repo.deleteById(id);

        return "redirect:/";
    }

}
