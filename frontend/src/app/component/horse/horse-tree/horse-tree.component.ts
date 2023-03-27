import {Component, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {HorseTreeNode} from '../../../dto/horse';
import {ActivatedRoute, Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {HorseService} from '../../../service/horse.service';
import {HttpErrorResponse} from '@angular/common/http';

@Component({
  selector: 'app-horse-tree',
  templateUrl: './horse-tree.component.html',
  styleUrls: ['./horse-tree.component.scss']
})
export class HorseTreeComponent implements OnInit {

  public horseTreeRoot?: Observable<HorseTreeNode>;
  public generations = 1;

  constructor(
    private service: HorseService,
    private router: Router,
    private route: ActivatedRoute,
    private notification: ToastrService,
  ) {
  }
  loadTree() {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    this.horseTreeRoot = this.service.tree(id, this.generations);
    this.horseTreeRoot.subscribe({
      error: (errorResponse: HttpErrorResponse) => {
        this.notification.error(`Could not find horse`);
        this.router.navigate(['/horses']);
        console.error('Error no horse found with id', this.route.snapshot.paramMap.get('id'));
        return;
      }
    });
  }

  ngOnInit(): void {
    if (Number.isNaN(this.route.snapshot.paramMap.get('id'))) {
      this.notification.error(`Invalid horse id given`);
      this.router.navigate(['/horses']);
      console.error('Error invalid horse id', this.route.snapshot.paramMap.get('id'));
      return;
    }

    this.loadTree();
  }

  deleteHorse(horse: HorseTreeNode) {
    this.service.delete(horse.id).subscribe({
      next: () => {
        this.notification.success(`Horse ${horse.name} successfully deleted.`);
        this.loadTree();
      },
      error: (errorResponse: HttpErrorResponse) => {
        this.notification.error(`Could not delete horse: ${errorResponse.error.errors}`);
        console.error('Error deleting horse', errorResponse.error.errors);
      }
    });
  }
}
