// Matches: leitores parecidos, com o anel de afinidade em volta do avatar

const usuario = montarLayout('matches');
const caixa = document.getElementById('matches');

carregar(caixa, async () => {
  const matches = await api(`/usuarios/${usuario.id}/matches`);
  if (matches.length === 0) {
    caixa.innerHTML = `
      <div class="card vazio-grande">
        <div class="emoji">💞</div>
        <p class="suave">Ainda sem matches. Curta experiências no Descobrir e marque livros como lidos!</p>
        <a href="descobrir.html" class="botao" style="margin-top:16px">🔥 Ir para o Descobrir</a>
      </div>`;
    return;
  }

  caixa.innerHTML = `<div class="grade-matches">${matches.map(m => `
    <div class="card match">
      <div class="anel" style="--pct:${m.afinidade}">
        <span class="avatar">${esc(iniciais(m.usuario.nome))}</span>
      </div>
      <h3>${esc(m.usuario.nome)}</h3>
      <p class="suave pequeno">${esc(m.usuario.curso || '')} ${m.usuario.bairro ? '· 📍 ' + esc(m.usuario.bairro) : ''}</p>
      <p class="afinidade">${m.afinidade}% de afinidade ❤️</p>
      <div class="chips" style="justify-content:center">
        ${m.livrosEmComum.map(l => `<a href="livro.html?id=${l.id}" class="chip">📖 ${esc(l.titulo)}</a>`).join('')}
      </div>
      <div class="chips" style="justify-content:center">
        ${m.generosEmComum.map(g => `<span class="chip chip-terracota">${esc(g.nome)}</span>`).join('')}
      </div>
    </div>`).join('')}</div>`;
});
