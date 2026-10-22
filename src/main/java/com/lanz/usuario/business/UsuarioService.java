package com.lanz.usuario.business;

import com.lanz.usuario.business.converter.UsuarioConverter;
import com.lanz.usuario.business.dto.EnderecoDTO;
import com.lanz.usuario.business.dto.TelefoneDTO;
import com.lanz.usuario.business.dto.UsuarioDTO;
import com.lanz.usuario.infrastructure.entity.Endereco;
import com.lanz.usuario.infrastructure.entity.Telefone;
import com.lanz.usuario.infrastructure.entity.Usuario;
import com.lanz.usuario.infrastructure.exceptions.ConflictExeption;
import com.lanz.usuario.infrastructure.exceptions.ResourceNotFoundException;
import com.lanz.usuario.infrastructure.repository.EnderecoRepository;
import com.lanz.usuario.infrastructure.repository.TelefoneRepository;
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
    private final EnderecoRepository enderecoRepository;
    private final TelefoneRepository telefoneRepository;

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

    public UsuarioDTO buscarUsuarioPorEmail(String email){
        try {
            return usuarioConverter.paraUsuarioDTO(
                    usuarioRepository.findByEmail(email).orElseThrow(
                    () -> new ResourceNotFoundException("Email não encontrado " + email)));
        }catch (ResourceNotFoundException e){
            throw new ResourceNotFoundException("Email não encontrado " + email);
        }
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

    public EnderecoDTO atualizaEndereco(Long idEndereco, EnderecoDTO enderecoDTO) {

        Endereco entity = enderecoRepository.findById(idEndereco).orElseThrow(
                () -> new ResourceNotFoundException("Id não encontrado " + idEndereco));

        Endereco endereco = usuarioConverter.updateEndereco(enderecoDTO, entity);

        return usuarioConverter.paraEnderecoDTO(enderecoRepository.save(endereco));
       }

       public TelefoneDTO atualizaTelefone(Long idTelefone, TelefoneDTO telefoneDTO){

           Telefone entity = telefoneRepository.findById(idTelefone).orElseThrow(
                   () -> new ResourceNotFoundException("Id não encontrado " + idTelefone));

           Telefone telefone = usuarioConverter.updateTelefone(telefoneDTO, entity);

           return usuarioConverter.paraTelefoneDTO(telefoneRepository.save(telefone));
    }

    public EnderecoDTO cadastraEndereco(String token, EnderecoDTO dto){
        String email = jwtUtil.extractUseremail(token.substring(7));
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("Email não encontrado " + email));

        Endereco endereco = usuarioConverter.paraEnderecoEntity(dto, usuario.getId());

        Endereco enderecoEntity = enderecoRepository.save(endereco);

        return usuarioConverter.paraEnderecoDTO(enderecoEntity);
    }

    public TelefoneDTO cadastraTelefone(String token, TelefoneDTO dto){
        String email = jwtUtil.extractUseremail(token.substring(7));
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("Email não encontrado " + email));

        Telefone telefone = usuarioConverter.paraTelefoneEntity(dto, usuario.getId());

        Telefone telefoneEntity = telefoneRepository.save(telefone);

        return usuarioConverter.paraTelefoneDTO(telefoneEntity);
    }
}
