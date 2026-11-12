const fulfillmentChoices = document.querySelectorAll('input[name="fulfillment"]');
const addressLabel = document.querySelector('[data-address]');
function updateDeliveryField() {
    const delivery = document.querySelector('input[name="fulfillment"]:checked')?.value === 'DELIVERY';
    addressLabel.hidden = !delivery;
    addressLabel.querySelector('textarea').required = delivery;
}
if (addressLabel) {
    fulfillmentChoices.forEach(choice => choice.addEventListener('change', updateDeliveryField));
    updateDeliveryField();
}
