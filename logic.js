const canvas = document.getElementById('picture');
const ctx = canvas.getContext('2d');


// Рисуем пример, когда R = 150
// треугольник
ctx.moveTo(50, 200);
ctx.lineTo(200, 350);
ctx.lineTo(200, 200);
ctx.closePath();
ctx.fillStyle = 'red'
ctx.fill();

// прямоугольник
ctx.lineTo(200, 275);
ctx.lineTo(350, 275);
ctx.lineTo(350, 200);
ctx.closePath();
ctx.fill();

// четверть круга
ctx.beginPath();
ctx.moveTo(200, 200);
ctx.lineTo(200, 125);
ctx.arc(200, 200, 150, -Math.PI / 2, 0);
ctx.closePath();
ctx.fill();

ctx.strokeStyle = "black";
ctx.beginPath();
//ось х
ctx.moveTo(0, 200);
ctx.lineTo(400, 200);
// Ось У
ctx.moveTo(200, 0);
ctx.lineTo(200, 400);
// Стрелочки к осям
ctx.moveTo(195, 5)
ctx.lineTo(200, 0);
ctx.moveTo(205, 5)
ctx.lineTo(200, 0);
ctx.moveTo(395, 195);
ctx.lineTo(400, 200);
ctx.moveTo(395, 205);
ctx.lineTo(400, 200);
ctx.stroke();

// Настройки для текста
ctx.fillStyle = 'black';
ctx.font = '14px Arial';
ctx.textAlign = 'center';
ctx.textBaseline = 'middle';

// Подписи на оси Y
ctx.fillText('R', 207, 42);        // R сверху
ctx.fillText('R/2', 213, 118);     // R/2
ctx.fillText('-R/2', 215, 283);    // -R/2
ctx.fillText('-R', 210, 360);      // -R снизу

// Подписи на оси X
ctx.fillText('-R', 40, 190);       // -R слева
ctx.fillText('-R/2', 120, 190);    // -R/2
ctx.fillText('R/2', 285, 190);     // R/2
ctx.fillText('R', 360, 190);       // R справа

// Подписи осей (x и y)
ctx.fillText('x', 390, 190);       // буква x на конце оси X
ctx.fillText('y', 210, 10);        // буква y на конце оси Y

// Настройки для штрихов
ctx.strokeStyle = 'black';
ctx.lineWidth = 1;

// Штрихи на оси X (вертикальные)
ctx.beginPath();
// -R
ctx.moveTo(50, 195);
ctx.lineTo(50, 205);
// -R/2
ctx.moveTo(125, 195);
ctx.lineTo(125, 205);
// R/2
ctx.moveTo(275, 195);
ctx.lineTo(275, 205);
// R
ctx.moveTo(350, 195);
ctx.lineTo(350, 205);
ctx.stroke();

// Штрихи на оси Y (горизонтальные)
ctx.beginPath();
// R
ctx.moveTo(195, 50);
ctx.lineTo(205, 50);
// R/2
ctx.moveTo(195, 125);
ctx.lineTo(205, 125);
// -R/2
ctx.moveTo(195, 275);
ctx.lineTo(205, 275);
// -R
ctx.moveTo(195, 350);
ctx.lineTo(205, 350);
ctx.stroke();


const tbody = document.getElementById('results-body');
const coord_X = document.getElementById('coord_X');
const coord_Y = document.getElementById('coord_Y');
const radius_R = document.getElementById('radius_R');
const endpoint = '/fcgi-bin/point-checker.jar';

function check_not_null(coord_Y) {
    return coord_Y !== "";
}

function check_number(coord_Y) {
    return !isNaN(coord_Y);
}

function check_range(coord_Y) {
    return (parseFloat(coord_Y) >= -3 && parseFloat(coord_Y) <= 5);
}

function input_is_correct(coord_Y) {
    return check_not_null(coord_Y) && check_number(coord_Y) && check_range(coord_Y);
}

function add_element_to_table(record) {
    const tr = document.createElement('tr');
    const date = new Date(record.timestamp);
    const cells = [
        record.x, record.y, record.r,
        record.isHit ? 'Попала' : 'Не попала',
        Number.isNaN(date.getTime()) ? record.timestamp : date.toLocaleString('ru-RU'),
        Number(record.executionTime).toFixed(3)
    ];
    cells.forEach(value => {
        const td = document.createElement('td');
        td.textContent = value;
        tr.appendChild(td);
    });
    tr.children[3].style.color = record.isHit ? 'green' : 'red';
    tr.children[3].style.fontWeight = 'bold';
    tbody.prepend(tr);
}

function showHistory(history) {
    tbody.replaceChildren();
    history.forEach(add_element_to_table);
}

// Начальную загрузку завершаем до отправки точки.
const historyLoaded = (async () => {
    try {
        const response = await fetch(`${endpoint}?action=history`, { cache: 'no-store' });
        if (!response.ok) throw new Error(`Ошибка HTTP: ${response.status}`);
        const data = await response.json();
        showHistory(data.history);
    } catch (err) {
        console.error('Не удалось загрузить историю:', err);
    }
})();

const form = document.getElementById('form_to_input');
form.addEventListener('submit', async function(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const x = formData.get('coord_X');
    const y = formData.get('coord_Y');
    const r = formData.get('radius_R');

    if (!input_is_correct(y, r)) {
        alert("Ошибка: введите корректные числа в диапазоне!");
        return;
    }
    try {
        await historyLoaded;
        const response = await fetch(endpoint, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({coord_x: x, coord_y: y, radius_r: r})
        });
        if (!response.ok) throw new Error(`Ошибка HTTP: ${response.status}`);
        const result_data = await response.json();
        showHistory(result_data.history);
    } catch (err) {
        console.error(err);
        alert("Не удалось получить ответ сервера");
    }
});