import {Component, Input} from '@angular/core';
import {HorseTreeComponent} from '../horse-tree.component';
import {HorseTreeNode} from '../../../../dto/horse';

@Component({
  selector: 'app-tree-node',
  templateUrl: './tree-node.component.html',
  styleUrls: ['./tree-node.component.scss']
})
export class TreeNodeComponent {

  @Input()
  horse?: HorseTreeNode;
  expanded = false;
  constructor(
    private horseTree: HorseTreeComponent
  ) {
  }

  delete() {
    if (this.horse) {
      this.horseTree.deleteHorse(this.horse);
    }
  }

  toggleExpand() {
    this.expanded = !this.expanded;
  }

  formattedBirthday(): string {
    if (this.horse) {
      return new Date(this.horse.dateOfBirth).toLocaleDateString();
    }
    return '';
  }

  sexIconClass(): string {
    if (this.horse) {
      return this.horse.sex === 'FEMALE' ? 'bi-gender-female' : 'bi-gender-male';
    }
    return '';
  }
}
