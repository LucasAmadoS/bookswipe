// Perfil: dados, números, gêneros (editáveis) e minhas experiências

const usuario = montarLayout('perfil');
const caixa = document.getElementById('perfil');

carregar(caixa, mostrar);

async function mostrar() {
  const [impacto, meusGeneros, todosGeneros, experiencias] = await Promise.all([
    api(`/usuarios/${usuario.id}/impacto`),
    api(`/usuarios/${usuario.id}/generos`),
    api('/generos'),
    api(`/usuarios/${usuario.id}/experiencias`),
  ]);
  const marcados = new Set(meusGeneros.map(g => g.id));

  caixa.innerHTML = `
    <div class="card secao">
      <div class="perfil-cabecalho">
        <span class="avatar grande">${esc(iniciais(usuario.nome))}</span>
        <div>
          <h1>${esc(usuario.nome)}</h1>
          <p class="suave">@${esc(usuario.email.split('@')[0])} · ${esc(usuario.curso || 'Leitor')} ${usuario.bairro ? '· 📍 ' + esc(usuario.bairro) : ''}</p>
          <div class="perfil-numeros">
            <div><strong>${impacto.livrosLidos}</strong><span class="suave pequeno">livros lidos</span></div>
            <div><strong>${impacto.favoritos}</strong><span class="suave pequeno">favoritos</span></div>
            <div><strong>${impacto.livrosReutilizados}</strong><span class="suave pequeno">reutilizados</span></div>
          </div>
        </div>
        <button class="botao botao-contorno botao-pequeno" id="sair" style="margin-left:auto">Sair da conta</button>
      </div>
    </div>

    <section class="secao">
      <div class="secao-cabecalho"><h2>Meus gêneros</h2><button class="botao-link" id="editar">Editar</button></div>
      <div class="chips" id="generos">
        ${meusGeneros.map(g => `<span class="chip">${esc(g.nome)}</span>`).join('') || '<span class="suave">Nenhum gênero escolhido.</span>'}
      </div>
    </section>

    <section class="secao">
      <div class="secao-cabecalho"><h2>Minhas experiências</h2><a href="publicar.html">+ Publicar</a></div>
      ${experiencias.length ? `<div class="prateleira">${experiencias.map(e => `
        <a href="livro.html?id=${e.livro.id}" class="livro-mini">
          ${capa(e.livro)}
          <strong>${esc(e.livro.titulo)}</strong>
          <small>${estrelas(e.nota)}</small><br>
          <small>❤️ ${e.totalGostei} · 💬 ${e.totalComentarios}</small>
        </a>`).join('')}</div>`
        : '<p class="suave">Você ainda não publicou nenhuma experiência.</p>'}
    </section>`;

  document.getElementById('sair').addEventListener('click', () => sessao.sair());

  // Editar gêneros: troca as etiquetas por botões marcáveis + Salvar
  document.getElementById('editar').addEventListener('click', () => {
    const area = document.getElementById('generos');
    area.innerHTML = todosGeneros.map(g =>
      `<button class="chip-botao ${marcados.has(g.id) ? 'ativo' : ''}" data-id="${g.id}">${esc(g.nome)}</button>`).join('')
      + '<button class="botao botao-pequeno" id="salvar">Salvar</button>';

    area.querySelectorAll('[data-id]').forEach(botao => botao.addEventListener('click', () => {
      const id = Number(botao.dataset.id);
      botao.classList.toggle('ativo');
      if (marcados.has(id)) marcados.delete(id); else marcados.add(id);
    }));

    document.getElementById('salvar').addEventListener('click', async () => {
      try {
        await api(`/usuarios/${usuario.id}/generos`, 'PUT', { generoIds: [...marcados] });
        aviso('Gêneros salvos! Seus matches foram atualizados.');
        mostrar();
      } catch (e) {
        aviso(e.message, 'erro');
      }
    });
  });
}
