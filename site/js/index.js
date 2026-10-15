// Landing page: logo, três capas de enfeite e o contador de impacto vindo da API

document.getElementById('logo').innerHTML = LOGO_SVG + '<span>BookSwipe</span>';

// Quem já está logado vai direto para o início
if (sessao.usuario()) {
  document.querySelector('.hero .botao').href = 'inicio.html';
}

const enfeite = [
  { titulo: '1984', autor: 'George Orwell' },
  { titulo: 'O Hobbit', autor: 'J.R.R. Tolkien' },
  { titulo: 'Dom Casmurro', autor: 'Machado de Assis' },
];
document.getElementById('hero-livros').innerHTML = enfeite.map(l => capa(l)).join('');

// Número real do banco (se a API estiver desligada, fica o "—")
api('/indicadores')
  .then(dados => {
    document.getElementById('total-reutilizados').textContent = dados.livrosReutilizados.toLocaleString('pt-BR');
  })
  .catch(() => {});
