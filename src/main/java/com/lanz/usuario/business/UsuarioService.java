package com.lanz.usuario.business;

import com.lanz.usuario.business.converter.UsuarioConverter;
import com.lanz.usuario.business.dto.UsuarioDTO;
import com.lanz.usuario.infrastructure.entity.Usuario;
import com.lanz.usuario.infrastructure.repository.UsuarioRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioConverter usuarioConverter;

    public UsuarioDTO salvaUsuario(UsuarioDTO usuarioDTO){
        Usuario usuario = usuarioConverter.paraUsuario(usuarioDTO);
        usuario = usuarioRepository.save(usuario);
        return usuarioConverter.paraUsuarioDTO(usuario);
    }
}
