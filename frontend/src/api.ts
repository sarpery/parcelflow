export interface Shipment {
    id: number;
    trackingNumber: string;
    senderName: string;
    recipientName: string;
    origin: string;
    destination: string;
    status: ShipmentStatus;
    createdAt: string;
    estimatedDelivery: string | null;
}

export type ShipmentStatus =
    | "CREATED"
    | "PICKED_UP"
    | "IN_TRANSIT"
    | "OUT_FOR_DELIVERY"
    | "DELIVERED"
    | "EXCEPTION"
    | "CANCELLED";

const API_URL = "http://localhost:8080/api";

export interface ShipmentPage {
    content: Shipment[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}

export async function getShipments(
    page = 0,
    size = 3,
    status?: ShipmentStatus
): Promise<ShipmentPage> {
    const params = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
    });

    if (status) {
        params.append("status", status);
    }

    const response = await fetch(
        `${API_URL}/shipments?${params.toString()}`
    );

    if (!response.ok) {
        throw new Error("Failed to fetch shipments");
    }

    return response.json();
}

export async function getShipment(id: number): Promise<Shipment> {
    const response = await fetch(`${API_URL}/shipments/${id}`);

    if (!response.ok) {
        throw new Error("Failed to fetch shipment");
    }

    return response.json();
}

export async function updateShipmentStatus(
    id: number,
    status: ShipmentStatus
): Promise<Shipment> {

    const response = await fetch(
        `${API_URL}/shipments/${id}/status`,
        {
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                status: status,
            }),
        }
    );

    if (!response.ok) {
        throw new Error("Failed to update shipment status");
    }

    return response.json();
}

