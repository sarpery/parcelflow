import { useState } from "react";
import {
    updateShipmentStatus,
    type Shipment,
    type ShipmentStatus,
} from "../api";

interface ShipmentDetailsProps {
    shipment: Shipment;
    onUpdated: (shipment: Shipment) => void;
    onBack: () => void;
}

const statuses: ShipmentStatus[] = [
    "CREATED",
    "PICKED_UP",
    "IN_TRANSIT",
    "OUT_FOR_DELIVERY",
    "DELIVERED",
    "EXCEPTION",
    "CANCELLED",
];

export default function ShipmentDetails({
                                            shipment,
                                            onUpdated,
                                            onBack,
                                        }: ShipmentDetailsProps) {

    const [newStatus, setNewStatus] =
        useState<ShipmentStatus>(shipment.status);

    const [updating, setUpdating] = useState(false);
    const [error, setError] = useState<string | null>(null);

    async function handleStatusUpdate() {

        if (newStatus === shipment.status) {
            return;
        }

        setUpdating(true);
        setError(null);

        try {
            const updatedShipment =
                await updateShipmentStatus(
                    shipment.id,
                    newStatus
                );

            onUpdated(updatedShipment);

        } catch {
            setError(
                "Could not update shipment status. " +
                "The requested transition may not be allowed."
            );
        } finally {
            setUpdating(false);
        }
    }

    return (
        <div>

            <button onClick={onBack}>
                ← Back to shipments
            </button>

            <h2>{shipment.trackingNumber}</h2>

            <p>
                <strong>Sender:</strong>{" "}
                {shipment.senderName}
            </p>

            <p>
                <strong>Recipient:</strong>{" "}
                {shipment.recipientName}
            </p>

            <p>
                <strong>Route:</strong>{" "}
                {shipment.origin} → {shipment.destination}
            </p>

            <p>
                <strong>Current status:</strong>{" "}
                {shipment.status}
            </p>

            <hr />

            <h3>Change status</h3>

            <select
                value={newStatus}
                onChange={(event) =>
                    setNewStatus(
                        event.target.value as ShipmentStatus
                    )
                }
                disabled={updating}
            >
                {statuses.map((status) => (
                    <option
                        key={status}
                        value={status}
                    >
                        {status}
                    </option>
                ))}
            </select>

            <button
                onClick={handleStatusUpdate}
                disabled={
                    updating ||
                    newStatus === shipment.status
                }
            >
                {updating
                    ? "Updating..."
                    : "Update status"}
            </button>

            {error && (
                <p>
                    {error}
                </p>
            )}

        </div>
    );
}