// Minha estante: abas Lendo / Lidos / Quero ler / Favoritos

const usuario = montarLayout('estante');
const ABAS = [
  { id: 'LENDO',     nome: '📖 Lendo' },
  { id: 'LIDO',      nome: '📚 Lidos' },
  { id: 'QUERO_LER', nome: '🔖 Quero ler' },
  { id: 'FAVORITOS', nome: '❤️ Favoritos' },
];
let abaAtual = parametro('aba') || 'LENDO';

desenharAbas();

async function desenharAbas() {
  const [totais, favoritos] = await Promise.all([
    api(`/usuarios/${usuario.id}/estante/totais`),
    api(`/usuarios/${usuario.id}/estante?aba=FAVORITOS`),
  ]).catch(e => { aviso(e.message, 'erro'); return [{}, []]; });
  totais.FAVORITOS = favoritos.length;

  const abas = document.getElementById('abas');
  abas.innerHTML = ABAS.map(a => `
    <button class="aba ${a.id === abaAtual ? 'ativa' : ''}" data-aba="${a.id}">
      ${a.nome} <span class="contador">${totais[a.id] ?? 0}</span>
    </button>`).join('');
  abas.querySelectorAll('.aba').forEach(botao => botao.addEventListener('click', () => {
    abaAtual = botao.dataset.aba;
    desenharAbas();
  }));
  mostrarLivros();
}

async function mostrarLivros() {
  const caixa = document.getElementById('livros');
  await carregar(caixa, async () => {
    const itens = await api(`/usuarios/${usuario.id}/estante?aba=${abaAtual}`);
    if (itens.length === 0) {
      caixa.innerHTML = `
        <div class="card vazio-grande">
          <div class="emoji">📚</div>
          <p class="suave">Nenhum livro nesta aba ainda.</p>
          <p class="suave pequeno">Use a busca lá em cima para achar um livro e adicionar à estante.</p>
        </div>`;
      return;
    }
    caixa.innerHTML = `<div class="grade">${itens.map(item => `
      <div class="livro-card">
        <a href="livro.html?id=${item.livro.id}">${capa(item.livro, 'cheia')}</a>
        <h3><a href="livro.html?id=${item.livro.id}">${esc(item.livro.titulo)}</a> ${item.favorito ? '❤️' : ''}</h3>
        <p class="suave pequeno">${esc(item.livro.autor)}</p>
        <button class="botao botao-perigo botao-pequeno" data-remover="${item.livro.id}">Tirar da estante</button>
      </div>`).join('')}</div>`;

    caixa.querySelectorAll('[data-remover]').forEach(botao => botao.addEventListener('click', async () => {
      if (!confirm('Tirar este livro da estante?')) return;
      try {
        await api(`/usuarios/${usuario.id}/estante/${botao.dataset.remover}`, 'DELETE');
        aviso('Livro retirado da estante');
        desenharAbas();
      } catch (e) {
        aviso(e.message, 'erro');
      }
    }));
  });
}
