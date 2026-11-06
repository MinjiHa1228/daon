document.addEventListener('DOMContentLoaded', () => {
  const nav = document.querySelector('.nav');
  if (!nav) return;
  const inferredAdmin = !!nav.querySelector('a[href="/admin/users"],a[href="/admin/audit"],a[href="/employee/list"]');
  const isAdmin = nav.dataset.admin ? nav.dataset.admin === 'true' : inferredAdmin;
  const links = [
    ['/product/list','재고 관리'],
    ['/order/list','수주·생산·출고'],
    ['/stock/movement','입출고 처리'],
    ['/stock/history','입출고 이력'],
    ...(isAdmin ? [
      ['/customer/list','거래처 관리'],
      ['/employee/list','직원 관리'],
      ['/admin/users','계정 관리'],
      ['/admin/audit','변경 이력']
    ] : [])
  ];
  const path = window.location.pathname;
  nav.innerHTML = `<div class="brand">다온테크 ERP</div><div class="common-nav-links">${links.map(([href,label]) => `<a href="${href}" class="${path===href || (href==='/product/list' && path.startsWith('/product/')) ? 'active' : ''}">${label}</a>`).join('')}</div>`;
  nav.classList.add('common-nav');
});
