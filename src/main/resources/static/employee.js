document.addEventListener('DOMContentLoaded', () => {
  const base = document.getElementById('baseSalary');
  const allowance = document.getElementById('allowance');
  const deduction = document.getElementById('deduction');
  const preview = document.getElementById('netSalaryPreview');
  if (!base || !allowance || !deduction || !preview) return;
  const calculate = () => {
    const net = Math.max(0, Number(base.value || 0) + Number(allowance.value || 0) - Number(deduction.value || 0));
    preview.value = net.toLocaleString('ko-KR') + '원';
  };
  [base, allowance, deduction].forEach(input => input.addEventListener('input', calculate));
  calculate();
  document.querySelectorAll('.payroll-edit').forEach(button => button.addEventListener('click', () => {
    document.getElementById('payMonth').value = button.dataset.month || '';
    base.value = button.dataset.base || 0; allowance.value = button.dataset.extra || 0; deduction.value = button.dataset.deduction || 0;
    document.getElementById('paymentStatus').value = button.dataset.status || 'PENDING';
    document.getElementById('paymentDate').value = button.dataset.date || '';
    document.getElementById('payrollMemo').value = button.dataset.memo || '';
    calculate(); document.getElementById('payroll-management').scrollIntoView({behavior:'smooth'});
  }));

  const calendar = document.getElementById('allowanceCalendar');
  if (calendar) {
    const [year, month] = calendar.dataset.month.split('-').map(Number);
    const records = new Map([...document.querySelectorAll('#workRecords i')].map(el => [el.dataset.date, el.dataset]));
    const weekdays = ['일','월','화','수','목','금','토'];
    calendar.className = 'allowance-calendar';
    weekdays.forEach(day => calendar.insertAdjacentHTML('beforeend', `<div class="cal-head">${day}</div>`));
    const first = new Date(year, month - 1, 1).getDay();
    const last = new Date(year, month, 0).getDate();
    for (let i = 0; i < first; i++) calendar.insertAdjacentHTML('beforeend', '<div class="cal-day empty"></div>');
    for (let day = 1; day <= last; day++) {
      const key = `${year}-${String(month).padStart(2,'0')}-${String(day).padStart(2,'0')}`;
      const r = records.get(key);
      const detail = r ? `<b>도마 ${Number(r.qty).toLocaleString()}개</b><span>도마수당 ${Number(r.board).toLocaleString()}원</span><span>야근 ${r.hours}시간</span><span>야근수당 ${Number(r.overtime).toLocaleString()}원</span><strong>${Number(r.total).toLocaleString()}원</strong>` : '';
      calendar.insertAdjacentHTML('beforeend', `<div class="cal-day${r?' worked':''}" data-date="${key}"><em>${day}</em>${detail}</div>`);
    }
    const entryForm = document.getElementById('workAllowanceForm');
    const entryModal = document.getElementById('workAllowanceModal');
    const closeEntryModal = () => { entryModal.hidden = true; document.body.style.overflow = ''; };
    document.getElementById('closeWorkAllowanceModal')?.addEventListener('click', closeEntryModal);
    entryModal?.addEventListener('click', event => { if (event.target === entryModal) closeEntryModal(); });
    document.addEventListener('keydown', event => { if (event.key === 'Escape' && !entryModal.hidden) closeEntryModal(); });
    calendar.addEventListener('click', event => {
      const cell = event.target.closest('.cal-day[data-date]');
      if (!cell) return;
      calendar.querySelectorAll('.cal-day.selected').forEach(el => el.classList.remove('selected'));
      cell.classList.add('selected');
      const date = cell.dataset.date;
      const record = records.get(date);
      document.getElementById('workDate').value = date;
      document.getElementById('boardQuantity').value = record?.qty || 0;
      document.getElementById('boardUnitPrice').value = record?.unit || 0;
      document.getElementById('overtimeHours').value = record?.hours || 0;
      document.getElementById('attended').value = record?.attended || 'true';
      document.getElementById('deleteWorkAllowance').hidden = !record;
      document.getElementById('workAllowanceFormTitle').textContent = `${date} 도마·야근 등록`;
      entryModal.hidden = false;
      document.body.style.overflow = 'hidden';
      document.getElementById('boardQuantity').focus();
    });
  }

  const params = new URLSearchParams(window.location.search);
  if (params.get('view') === 'payroll') {
    document.getElementById('payroll-management')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  } else if (params.get('view') === 'allowance') {
    document.getElementById('allowance-management')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  } else if (params.get('view') === 'info') {
    document.getElementById('employee-detail')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }
});
