package com.lanz.usuario.business;

import com.lanz.usuario.business.converter.UsuarioConverter;
import com.lanz.usuario.business.dto.UsuarioDTO;
import com.lanz.usuario.infrastructure.entity.Usuario;
import com.lanz.usuario.infrastructure.exceptions.ConflictExeption;
import com.lanz.usuario.infrastructure.exceptions.ResourceNotFoundException;
import com.lanz.usuario.infrastructure.repository.UsuarioRepository;
import com.lanz.usuario.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioConverter usuarioConverter;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UsuarioDTO salvaUsuario(UsuarioDTO usuarioDTO){
        emailExiste(usuarioDTO.getEmail());
        usuarioDTO.setSenha(passwordEncoder.encode(usuarioDTO.getSenha()));
        Usuario usuario = usuarioConverter.paraUsuario(usuarioDTO);
        usuario = usuarioRepository.save(usuario);
        return usuarioConverter.paraUsuarioDTO(usuario);
    }

    public void emailExiste(String email){
        try{
            boolean existe = verificaEmailExistente(email);
            if(existe){
                throw new ClassCastException("Email já cadastrado " + email);
            }
        }catch (ConflictExeption e){
            throw new ConflictExeption("Email já cadastrado", e.getCause());
        }
    }

    public boolean verificaEmailExistente(String email){
        return usuarioRepository.existsByEmail(email);
    }

    public Usuario buscarUsuarioPorEmail(String email){
        return usuarioRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("Email não encontrado " + email)
        );
    }

    public void deletaUsuarioPorEmail(String email){
        usuarioRepository.deleteByEmail(email);
    }

    public UsuarioDTO atualizaDadosUsuario(String token, UsuarioDTO dto){
        //Busca o email do usuário através dp token (tirar obrigatoriedade de email)
        String email = jwtUtil.extractUseremail(token.substring(7));

        //Criptografia de senha
        dto.setSenha(dto.getSenha() != null ? passwordEncoder.encode(dto.getSenha()) : null);

         //Busca os dados do usuário no banco de dados
         Usuario usuarioEntity = usuarioRepository.findByEmail(email).orElseThrow(
                 () -> new ResourceNotFoundException("Email não localizado"));

         //Mescla os dados recebidos na requisição DTO com os dados do banco de dados
         Usuario usuario = usuarioConverter.updateUsuario(dto, usuarioEntity);

         //Salva os dados do usuário convertido e depois com os retornos converte para usuárioDTO
         return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));
    }
}
