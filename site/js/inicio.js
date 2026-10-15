// Início: números do usuário, livros em alta, uma experiência "para você" e o que está lendo

const usuario = montarLayout('inicio');
document.getElementById('ola').textContent = `Olá, ${usuario.nome.split(' ')[0]}! 👋`;

carregarNumeros();
carregarEmAlta();
carregarParaVoce();
carregarLendo();

async function carregarNumeros() {
  const caixa = document.getElementById('numeros');
  await carregar(caixa, async () => {
    const [meu, totais] = await Promise.all([          // as duas chamadas ao mesmo tempo
      api(`/usuarios/${usuario.id}/impacto`),
      api(`/usuarios/${usuario.id}/estante/totais`),
    ]);
    caixa.innerHTML = `
      <div class="numero-card"><div class="valor">${meu.livrosLidos}</div><div class="suave">📚 livros lidos</div></div>
      <div class="numero-card"><div class="valor">${totais.LENDO}</div><div class="suave">📖 lendo agora</div></div>
      <div class="numero-card destaque"><div class="valor">${meu.livrosReutilizados}</div><div>♻️ livros reutilizados</div></div>`;
  });
}

// "Em alta" vem do indicador (título + total de ❤). Cruzo com a lista de livros para ter id e capa.
async function carregarEmAlta() {
  const caixa = document.getElementById('em-alta');
  await carregar(caixa, async () => {
    const [indicadores, livros] = await Promise.all([api('/indicadores'), api('/livros')]);
    const porTitulo = new Map(livros.map(l => [l.titulo, l]));
    caixa.innerHTML = indicadores.emAlta
      .map(item => porTitulo.get(item.rotulo))
      .filter(Boolean)
      .map(livro => cartaoLivroMini(livro)).join('');
  });
}

async function carregarParaVoce() {
  const caixa = document.getElementById('para-voce');
  await carregar(caixa, async () => {
    const fila = await api(`/usuarios/${usuario.id}/descobrir`);
    if (fila.length === 0) {
      caixa.innerHTML = '<div class="card vazio">Você já viu todas as experiências novas! ✨</div>';
      return;
    }
    const exp = fila[0];
    caixa.innerHTML = `
      <div class="card" style="display:flex;gap:22px;align-items:center;flex-wrap:wrap">
        <a href="livro.html?id=${exp.livro.id}">${capa(exp.livro)}</a>
        <div style="flex:1;min-width:220px">
          <h3><a href="livro.html?id=${exp.livro.id}">${esc(exp.livro.titulo)}</a></h3>
          <p class="suave pequeno">${esc(exp.livro.autor)}</p>
          <p>${estrelas(exp.nota)} <span class="suave pequeno">por ${esc(exp.usuario.nome)}</span></p>
          <p class="suave" style="margin-top:6px">“${esc(exp.texto)}”</p>
        </div>
        <div class="botoes-swipe">
          <button class="botao-redondo passar" data-tipo="PASSAR" title="Passar">✕</button>
          <button class="botao-redondo gostei" data-tipo="GOSTEI" title="Gostei">♥</button>
        </div>
      </div>`;
    caixa.querySelectorAll('[data-tipo]').forEach(botao => {
      botao.addEventListener('click', async () => {
        try {
          await api(`/experiencias/${exp.id}/avaliacao`, 'POST', { tipo: botao.dataset.tipo });
          if (botao.dataset.tipo === 'GOSTEI') aviso('❤️ Você gostou!');
          carregarParaVoce(); // mostra a próxima
        } catch (e) {
          aviso(e.message, 'erro');
        }
      });
    });
  });
}

async function carregarLendo() {
  const caixa = document.getElementById('lendo');
  await carregar(caixa, async () => {
    const itens = await api(`/usuarios/${usuario.id}/estante?aba=LENDO`);
    caixa.innerHTML = itens.length
      ? itens.map(item => cartaoLivroMini(item.livro)).join('')
      : '<p class="suave">Nenhum livro em leitura. <a href="estante.html" style="color:var(--verde-escuro)">Escolha o próximo!</a></p>';
  });
}

function cartaoLivroMini(livro) {
  return `<a href="livro.html?id=${livro.id}" class="livro-mini">
            ${capa(livro)}
            <strong>${esc(livro.titulo)}</strong>
            <small>${esc(livro.autor)}</small>
          </a>`;
}
