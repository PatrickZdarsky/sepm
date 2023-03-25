import {Component, OnInit} from '@angular/core';
import {ToastrService} from 'ngx-toastr';
import {OwnerService} from 'src/app/service/owner.service';
import {Owner} from 'src/app/dto/owner';


@Component({
  selector: 'app-owner',
  templateUrl: './owner.component.html',
  styleUrls: ['./owner.component.scss']
})
export class OwnerComponent implements OnInit {

  owners: Owner[] = [];
  bannerError: string | null = null;

  constructor(
    private ownerService: OwnerService,
    private notification: ToastrService,
  ) { }

  ngOnInit(): void {
    this.reloadList();
  }

  reloadList() {
    this.ownerService.searchByName(undefined, undefined)
      .subscribe({
        next: data => {
          this.owners = data;
        },
        error: error => {
          console.error('Error fetching owners', error);
          this.bannerError = 'Could not fetch owners: ' + error.message;
          const errorMessage = error.status === 0
            ? 'Is the backend up?'
            : error.message.message;
          this.notification.error(errorMessage, 'Could Not Fetch Owners');
        }
      });
  }

  ownerName(owner: Owner | null): string {
    return owner
      ? `${owner.firstName} ${owner.lastName}`
      : '';
  }
}
