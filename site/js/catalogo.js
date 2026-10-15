// =============================================================
// catalogo.js - "Não achou? Buscar na internet"
// Abre uma janela que pesquisa livros na API pública (pela nossa API Java),
// deixa escolher o gênero e salva o livro no BookSwipe já com capa e ISBN.
//
// Uso: abrirBuscaNaInternet('texto inicial', livro => { ...o que fazer com o livro salvo... })
// =============================================================

async function abrirBuscaNaInternet(textoInicial, aoImportar) {
  const generos = await api('/generos').catch(() => []);

  const fundo = document.createElement('div');
  fundo.className = 'modal-fundo';
  fundo.innerHTML = `
    <div class="modal" style="max-width:620px" role="dialog" aria-label="Buscar livro na internet">
      <div class="secao-cabecalho"><h2>🌐 Buscar na internet</h2><button class="botao-link" id="fechar-busca">Fechar</button></div>
      <form id="form-busca-externa" style="display:flex;gap:8px;margin-bottom:6px">
        <input type="search" id="texto-externo" class="campo-externo" placeholder="Título ou autor..." value="${esc(textoInicial || '')}"
               style="flex:1;padding:11px 14px;border:1.5px solid var(--borda);border-radius:10px;font:inherit">
        <button class="botao botao-pequeno" type="submit">Buscar</button>
      </form>
      <p class="suave pequeno" id="fonte-externa" style="margin-bottom:12px"></p>
      <div id="resultados-externos"></div>
    </div>`;
  document.body.appendChild(fundo);

  const fechar = () => fundo.remove();
  fundo.querySelector('#fechar-busca').addEventListener('click', fechar);
  fundo.addEventListener('click', e => { if (e.target === fundo) fechar(); });

  const caixa = fundo.querySelector('#resultados-externos');
  const campo = fundo.querySelector('#texto-externo');

  const buscar = async () => {
    await carregar(caixa, async () => {
      const resposta = await api('/catalogo-externo?busca=' + encodeURIComponent(campo.value.trim()));
      fundo.querySelector('#fonte-externa').textContent = `Resultados da ${resposta.fonte}`;
      const lista = resposta.resultados;
      if (lista.length === 0) {
        caixa.innerHTML = '<p class="vazio">Nada encontrado. Tente outro texto.</p>';
        return;
      }
      caixa.innerHTML = lista.map((s, i) => `
        <div class="pedido">
          ${capa({ titulo: s.titulo, autor: s.autor, capaUrl: s.capaUrl }, 'mini')}
          <div class="pedido-info">
            <strong>${esc(s.titulo)}</strong>
            <div class="suave pequeno">${esc(s.autor || 'Autor desconhecido')} ${s.isbn ? '· ISBN ' + esc(s.isbn) : ''}</div>
          </div>
          <button class="botao botao-pequeno" data-indice="${i}">Escolher</button>
        </div>`).join('');

      caixa.querySelectorAll('[data-indice]').forEach(botao => botao.addEventListener('click', () => {
        escolherGenero(lista[Number(botao.dataset.indice)]);
      }));
    });
  };

  // Depois de escolher o livro: qual gênero? -> salva
  const escolherGenero = sugestao => {
    caixa.innerHTML = `
      <div style="display:flex;gap:16px;align-items:center;margin-bottom:16px">
        ${capa({ titulo: sugestao.titulo, autor: sugestao.autor, capaUrl: sugestao.capaUrl })}
        <div><h3>${esc(sugestao.titulo)}</h3><p class="suave">${esc(sugestao.autor || '')}</p></div>
      </div>
      <p style="margin-bottom:10px">Qual é o gênero deste livro?</p>
      <div class="chips" id="generos-externos" style="margin-bottom:18px">
        ${generos.map(g => `<button class="chip-botao" data-genero="${g.id}">${esc(g.nome)}</button>`).join('')}
      </div>
      <button class="botao botao-largo" id="salvar-externo" disabled>Adicionar ao BookSwipe</button>`;

    let generoId = 0;
    caixa.querySelectorAll('[data-genero]').forEach(botao => botao.addEventListener('click', () => {
      caixa.querySelectorAll('[data-genero]').forEach(b => b.classList.toggle('ativo', b === botao));
      generoId = Number(botao.dataset.genero);
      caixa.querySelector('#salvar-externo').disabled = false;
    }));

    caixa.querySelector('#salvar-externo').addEventListener('click', async () => {
      try {
        // Manda só os campos que a API espera (o record Importacao): campo a mais = JSON recusado
        const livro = await api('/livros/importar', 'POST', {
          titulo: sugestao.titulo,
          autor: sugestao.autor,
          isbn: sugestao.isbn,
          sinopse: sugestao.sinopse,
          capaUrl: sugestao.capaUrl,
          chaveExterna: sugestao.chaveExterna,
          generoId,
        });
        aviso(`📚 ${livro.titulo} foi adicionado ao BookSwipe!`);
        fechar();
        aoImportar(livro);
      } catch (e) {
        aviso(e.message, 'erro'); // ex: "Este livro já está cadastrado no BookSwipe"
      }
    });
  };

  fundo.querySelector('#form-busca-externa').addEventListener('submit', e => {
    e.preventDefault();
    buscar();
  });
  campo.focus();
  if (campo.value.trim().length >= 2) buscar();
}
