package org.ex9.authservice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Контроллер для обработки страниц входа и успешной авторизации.
 * Используется для отображения HTML-страниц (через Thymeleaf).
 * Состоит из страницы логина с формой авторизации и страницу успешной авторизации с отображением JWT-токена
 *
 * @author Краковцев Артём
 */
@Controller
public class LoginController {

    /**
     * Обрабатывает GET-запрос на страницу входа.
     *
     * @return имя шаблона HTML-страницы логина (login.html)
     */
    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    /**
     * Отображает страницу после успешной авторизации.
     * JWT-токен передаётся как параметр запроса и передаётся в шаблон.
     *
     * @param token JWT-токен, выданный после успешной авторизации
     * @param model объект для передачи данных в шаблон Thymeleaf
     * @return имя шаблона HTML-страницы (success.html)
     */
    @GetMapping("/login/success")
    public String oauthSuccessPage(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "success";
    }

}
