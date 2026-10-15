// Meu impacto: números, gráfico de trocas por mês, ranking, gêneros e em alta.
// Os gráficos são feitos só com HTML/CSS: a altura de cada barra é proporcional ao valor.

const usuario = montarLayout('impacto');
const painel = document.getElementById('painel');
const MESES = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];

carregar(painel, async () => {
  const [meu, comunidade] = await Promise.all([
    api(`/usuarios/${usuario.id}/impacto`),
    api('/indicadores'),
  ]);

  const maiorMes = Math.max(1, ...comunidade.trocasPorMes.map(m => m.total));
  const maiorGenero = Math.max(1, ...comunidade.generosMaisLidos.map(g => g.total));

  painel.innerHTML = `
    <div class="numeros">
      <div class="numero-card destaque">
        <div class="valor">${meu.livrosReutilizados}</div>
        <div>livros reutilizados por você</div>
      </div>
      <div class="numero-card">
        <div class="valor">${comunidade.livrosReutilizados}</div>
        <div class="suave">♻️ livros reutilizados na comunidade</div>
      </div>
      <div class="numero-card">
        <div class="valor">${comunidade.taxaReuso}%</div>
        <div class="suave">taxa de reuso (trocados ÷ oferecidos)</div>
      </div>
      <div class="numero-card">
        <div class="valor">${String(comunidade.engajamento).replace('.', ',')}</div>
        <div class="suave">interações por experiência</div>
      </div>
    </div>

    <div class="duas-colunas secao">
      <div class="card">
        <h2>📚 Livros reutilizados por mês</h2>
        <div class="grafico-barras">
          ${comunidade.trocasPorMes.map(m => `
            <div class="barra-coluna">
              <span class="barra-valor">${m.total}</span>
              <div class="barra" style="height:${(m.total / maiorMes) * 75}%"></div>
              <span class="barra-rotulo">${nomeDoMes(m.rotulo)}</span>
            </div>`).join('') || '<p class="suave">Nenhuma troca concluída ainda.</p>'}
        </div>
      </div>

      <div class="card">
        <h2 style="margin-bottom:6px">🏆 Comunidade</h2>
        <p class="suave pequeno" style="margin-bottom:8px">Quem mais participou de trocas</p>
        ${comunidade.ranking.map((r, i) => `
          <div class="ranking-item">
            <span class="ranking-posicao">${['🥇', '🥈', '🥉'][i] || i + 1}</span>
            <span class="avatar">${esc(iniciais(r.rotulo))}</span>
            <strong style="flex:1">${esc(r.rotulo)}</strong>
            <span class="suave pequeno">${r.total} trocas</span>
          </div>`).join('')}
      </div>
    </div>

    <div class="duas-colunas">
      <div class="card">
        <h2 style="margin-bottom:16px">Gêneros mais lidos</h2>
        ${comunidade.generosMaisLidos.map(g => `
          <div class="linha-genero">
            <span class="nome">${esc(g.rotulo)}</span>
            <div class="barra-horizontal"><span style="width:${(g.total / maiorGenero) * 100}%"></span></div>
            <strong>${g.total}</strong>
          </div>`).join('')}
      </div>
      <div class="card">
        <h2 style="margin-bottom:8px">🔥 Em alta</h2>
        ${comunidade.emAlta.map(l => `
          <div class="ranking-item">
            <span style="flex:1">📖 ${esc(l.rotulo)}</span>
            <span class="suave pequeno">❤️ ${l.total}</span>
          </div>`).join('')}
      </div>
    </div>`;
});

// "2026-06" -> "Jun/26"
function nomeDoMes(rotulo) {
  const [ano, mes] = rotulo.split('-');
  return `${MESES[Number(mes) - 1]}/${ano.slice(2)}`;
}
