using System.ComponentModel.DataAnnotations;

namespace Uturn.Models
{
	public class Task
	{
        public string employeeUid { get; set; } = "";

        public string vehicleId { get; set; } = "";

        [Required]
        public string type { get; set; } = "";

        [Required]
        public string typeOfGoods { get; set; } = "";

        [Required]
        public string pickupLocation { get; set; } = "";

        [Required]
        public string dropoffLocationId { get; set; } = "";

        public string ratingId { get; set; } = "";

        public string status { get; set; } = "Uploaded";

        public long uploadedTimestamp { get; set; } = 0;

        public long assignedTimestamp { get; set; } = 0;

        public long completedTimestamp { get; set; } = 0;
    }
}

