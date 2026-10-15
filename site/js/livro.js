// Página do livro: capa, nota média, botões da estante e experiências dos leitores.
// O livro vem pela URL: livro.html?id=3

const usuario = montarLayout('');
const livroId = Number(parametro('id'));
const caixa = document.getElementById('livro');

carregar(caixa, mostrar);

async function mostrar() {
  const [livro, experiencias, estante] = await Promise.all([
    api(`/livros/${livroId}`),
    api(`/livros/${livroId}/experiencias`),
    api(`/usuarios/${usuario.id}/estante`),
  ]);
  document.title = `${livro.titulo} · BookSwipe`;

  const media = experiencias.length
    ? experiencias.reduce((soma, e) => soma + e.nota, 0) / experiencias.length
    : 0;
  const naEstante = estante.find(item => item.livro.id === livroId); // pode ser undefined
  const jaPubliquei = experiencias.some(e => e.usuario.id === usuario.id);

  const botaoStatus = (status, texto) =>
    `<button class="botao botao-contorno botao-pequeno ${naEstante?.status === status ? 'marcado' : ''}" data-status="${status}">${texto}</button>`;

  caixa.innerHTML = `
    <div class="livro-cabecalho">
      ${capa(livro, 'grande')}
      <div class="livro-info">
        <h1>${esc(livro.titulo)}</h1>
        <p class="autor">${esc(livro.autor)}</p>
        <div class="livro-nota">
          ${estrelas(media)} <strong>${media.toFixed(1)}</strong>
          <span class="suave pequeno">/ 5 · ${experiencias.length} experiência(s)</span>
        </div>
        ${livro.genero ? `<span class="chip">${esc(livro.genero.nome)}</span>` : ''}

        <div class="botoes-estante">
          ${botaoStatus('QUERO_LER', '🔖 Quero ler')}
          ${botaoStatus('LENDO', '📖 Lendo')}
          ${botaoStatus('LIDO', '✅ Já li')}
          <button class="botao botao-contorno botao-pequeno ${naEstante?.favorito ? 'marcado' : ''}" id="favorito">
            ${naEstante?.favorito ? '❤️ Favorito' : '🤍 Favoritar'}
          </button>
        </div>
        <div class="botoes-estante">
          ${jaPubliquei ? '' : `<a href="publicar.html?livro=${livroId}" class="botao botao-destaque botao-pequeno">✍️ Contar minha experiência</a>`}
          <a href="trocas.html" class="botao botao-escuro botao-pequeno">♻️ Ver livros para troca</a>
          ${livro.capaUrl ? '' : '<button class="botao botao-contorno botao-pequeno" id="buscar-capa">🌐 Buscar capa na internet</button>'}
        </div>
      </div>
    </div>

    <div class="abas">
      <button class="aba ativa" data-aba="experiencias">Experiências <span class="contador">${experiencias.length}</span></button>
      <button class="aba" data-aba="sobre">Sobre o livro</button>
    </div>

    <div id="aba-experiencias" class="card">
      ${experiencias.length
        ? experiencias.map(htmlExperiencia).join('')
        : '<p class="vazio">Ninguém publicou ainda. Seja o primeiro!</p>'}
    </div>
    <div id="aba-sobre" class="card" hidden>
      <p>${livro.sinopse ? esc(livro.sinopse) : '<span class="suave">Sem sinopse cadastrada.</span>'}</p>
      ${livro.isbn ? `<p class="suave pequeno" style="margin-top:12px">ISBN: ${esc(livro.isbn)}</p>` : ''}
    </div>`;

  // Abas
  caixa.querySelectorAll('.aba').forEach(aba => aba.addEventListener('click', () => {
    caixa.querySelectorAll('.aba').forEach(a => a.classList.toggle('ativa', a === aba));
    document.getElementById('aba-experiencias').hidden = aba.dataset.aba !== 'experiencias';
    document.getElementById('aba-sobre').hidden = aba.dataset.aba !== 'sobre';
  }));

  // Botões da estante
  caixa.querySelectorAll('[data-status]').forEach(botao => botao.addEventListener('click', async () => {
    try {
      await api(`/usuarios/${usuario.id}/estante/${livroId}`, 'PUT', { status: botao.dataset.status });
      aviso('Estante atualizada 📚');
      mostrar();
    } catch (e) {
      aviso(e.message, 'erro');
    }
  }));

  // Livro sem capa: pede para a API Java procurar na API pública
  document.getElementById('buscar-capa')?.addEventListener('click', async evento => {
    evento.target.disabled = true;
    evento.target.textContent = 'Buscando…';
    try {
      await api(`/livros/${livroId}/completar`, 'POST');
      aviso('🖼️ Capa encontrada!');
      mostrar();
    } catch (e) {
      aviso(e.message, 'erro');
      evento.target.disabled = false;
      evento.target.textContent = '🌐 Buscar capa na internet';
    }
  });

  document.getElementById('favorito').addEventListener('click', async () => {
    try {
      const resposta = await api(`/usuarios/${usuario.id}/estante/${livroId}/favorito`, 'POST');
      aviso(resposta.favorito ? '❤️ Adicionado aos favoritos' : 'Removido dos favoritos');
      mostrar();
    } catch (e) {
      aviso(e.message, 'erro');
    }
  });
}

function htmlExperiencia(e) {
  return `
    <div class="experiencia">
      <div class="experiencia-topo">
        <span class="avatar">${esc(iniciais(e.usuario.nome))}</span>
        <div>
          <strong>${esc(e.usuario.nome)}</strong> ${estrelas(e.nota)}
          <div class="suave pequeno">${data(e.dataPost)} ${e.sentimento ? '· ' + esc(e.sentimento) : ''}</div>
        </div>
      </div>
      <p>“${esc(e.texto)}”</p>
      ${e.trechoFavorito ? `<p class="trecho">“${esc(e.trechoFavorito)}”</p>` : ''}
      <div class="interacoes"><span>❤️ ${e.totalGostei}</span><span>💬 ${e.totalComentarios}</span></div>
    </div>`;
}
