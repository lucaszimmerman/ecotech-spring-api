package com.ecotech.api.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ecotech.api.model.User;
import com.ecotech.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Implementação do UserDetailsService do Spring Security.
 *
 * Esta classe é responsável por informar ao Spring Security como localizar
 * um usuário durante o processo de autenticação.
 *
 * Sempre que alguém tentar fazer login, o Spring chamará automaticamente
 * o método loadUserByUsername(), passando o username informado pelo usuário.
 *
 * Nosso papel é:
 * 1. Buscar o usuário no banco de dados;
 * 2. Lançar uma exceção caso ele não exista;
 * 3. Converter nossa entidade User em um UserPrincipal, que é o formato
 *    compreendido pelo Spring Security.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado." ));

                return new UserPrincipal(user);
    }

    
}
